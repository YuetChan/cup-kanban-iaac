import { HttpStatus, Injectable, InternalServerErrorException } from "@nestjs/common";

import axios from 'axios';

import { User } from "src/model/user.model";

@Injectable()
export class RegisterService {

	private REST_HOST = '';

  constructor() { this.REST_HOST = process.env.REST_HOST; }

  async register(user: User): Promise<User> {
		console.trace('Enter register(user)');
		return this.getOrCreateUserByEmail(user);
  }

	async getOrCreateUserByEmail(user: User) {
		console.debug('Get user by user email');

		return axios.get(`${this.REST_HOST}/users`, { 
			params: { email: user.email } 
		}).then(async res => {
			console.debug('Get role by id');

			const user = res.data.data.user;
			const role = await this.getRoleById(user.id)

			return {
				id: user.id,

				email: user.email as string,
				name: user.name as string,
				role: role
			};
		}).catch(async err => {
			console.debug('Create user');
			const _user = await this.createUser(user)
			
			return {
				id: _user.id,
				email: user.email,
				name: user.name,
				role: 'user'
			}
		});
	}

	async getRoleById(id): Promise<string> {
		return await axios.get(`${this.REST_HOST}/users/${id}/role`).then(res => {
			if(res.status === HttpStatus.OK) { return res.data.data.role; }
				throw new InternalServerErrorException();
		}).catch(err => {
				console.debug('Err', err);
				throw new InternalServerErrorException();
		});
	}

	async createUser(user): Promise<User> {
		return await axios.post<{ data: { user: any } }>(`${this.REST_HOST}/users`, {
			data: {
				user: {
					email: user.email,
					role: 'user'
				}
			}
		}).then(res => {
			if(res.status !== HttpStatus.CREATED) { throw new InternalServerErrorException(); }
			return res.data.data.user;
		}).catch(err => {
			console.debug('Err', err);
			throw new InternalServerErrorException();
		});
	}

}
